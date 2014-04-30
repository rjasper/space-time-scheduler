function Om_st = calc_st_space(Om, path)

N_Om = size(Om, 2);
N_path = size(path, 2);
N_segs = N_path - 1;

v = cellfun(@calc_velocity, {Om{:}.path}, 'UniformOutput', false);
l = calc_path_length(path);
s_i = cumsum([0 l]);

Om_st = cellfun(@calc_Om_st, ...
    {Om{:}.polygon}, ...
    {Om{:}.path}, ...
    v, ...
    'UniformOutput', false);

Om_st = [Om_st{:}];

    function v = calc_velocity(path)
        dpath = diff(path, 1, 2);
        
        % v := [x; y] / t
        v = dpath(1:2, :) ./ repmat(dpath(3, :), 2, 1);
    end

    function Om_st = calc_Om_st(polygon, path_Om, v)
        n_v = size(v, 2);
        n_poly = size(polygon, 2);
        
        p1 = path_Om(:, 1:end-1);
        p2 = path_Om(:, 2:end);
        
        Om_st = cellfun(@calc_Om_st_v, ...
            mat2cell( p1, 3, ones(1, n_v) ), ...
            mat2cell( p2, 3, ones(1, n_v) ), ...
            mat2cell( v , 2, ones(1, n_v) ), ...
            'UniformOutput', false);
        
        Om_st = [Om_st{:}];
        
        function Om_st_j = calc_Om_st_v(p1, p2, v_j)
            xy1 = p1(1:2);
            xy2 = p2(1:2);
            t1 = p1(3);
            t2 = p2(3);
            dt = t2 - t1; % TODO: already calculated by calc_velocity
            
            polygon_j = polygon + repmat(xy1, 1, n_poly);
            
            Om_st_j = cell(1, N_segs);
            
            % for each path segment
            for i = 1:N_segs
                % TODO: consider parallel velocity vector
                
                path_i1 = path(:, i); % segment start
                path_i2 = path(:, i+1); % segment end
                path_i = [path_i1; path_i2]; % path segment
                vec_i = line2vec(path_i);
                
                cut_smin = repmat(path_i1, 2, 1) + [zeros(2, 1); -v_j];
                cut_smax = repmat(path_i2, 2, 1) + [zeros(2, 1); -v_j];
                cut_tmin = path_i;
                cut_tmax = path_i + repmat(dt * -v_j, 2, 1);
                
                % TODO: cut smin/smax only once per i
                
                if vec_vec_angle(vec_i, v_j) > pi % polygon comes from the left
                    [~, polygon_cut] = cut_polygon(polygon_j, cut_smin);
                    [polygon_cut, ~] = cut_multipolygon(polygon_cut, cut_smax);
                    [polygon_cut, ~] = cut_multipolygon(polygon_cut, cut_tmin);
                    [~, polygon_cut] = cut_multipolygon(polygon_cut, cut_tmax);
                else % vec_vec_angle(vec_i, v_j) < pi % polygon comes from the right
                    [polygon_cut, ~] = cut_polygon(polygon_j, cut_smin);
                    [~, polygon_cut] = cut_multipolygon(polygon_cut, cut_smax);
                    [~, polygon_cut] = cut_multipolygon(polygon_cut, cut_tmin);
                    [polygon_cut, ~] = cut_multipolygon(polygon_cut, cut_tmax);
                end
                
                e_s = vec_i / l(i);
                
                Om_st_ji = cellfun(@(p) ...
                    calc_polygon_st(p, path_i1, [s_i(i); t1], e_s, v_j), ...
                    polygon_cut, ...
                    'UniformOutput', false);
                
                % TODO: glue polygons
                
                Om_st_j{i} = Om_st_ji;
            end
            
            Om_st_j = [Om_st_j{:}];
        end
    end

    function P_st = calc_polygon_st(P, xy0, st0, e_s, v)
        n_P = size(P, 2);
        
        P_st = [e_s -v] \ (P - repmat(xy0, 1, n_P)) + repmat(st0, 1, n_P);
    end
end

function [P_L, P_R] = cut_multipolygon(P, cut)
if isempty(P)
    P_L = {};
    P_R = {};
    return;
end

[P_L, P_R] = cellfun(@(p) cut_polygon(p, cut), P, 'UniformOutput', false);

P_L = [P_L{:}];
P_R = [P_R{:}];
end