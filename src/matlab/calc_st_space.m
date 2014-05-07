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
            
            V_st = cell(1, N_segs);
            vid = cell(1, N_segs);
            vid_S_smin = cell(1, N_segs);
            vid_S_smax = cell(1, N_segs);
            
            % for each path segment
            for i = 1:N_segs
                % TODO: consider parallel velocity vector
                
                path_i1 = path(:, i); % segment start
                path_i2 = path(:, i+1); % segment end
                path_i = [path_i1; path_i2]; % path segment
                vec_i = line2vec(path_i);
                
                % TODO: define epsilon > 0
                if vec_vec_angle(vec_i, v_j) < pi % polygon goes to the left
                    direction = 'left';
                elseif vec_vec_angle(vec_i, v_j) > pi % polygon goes to the right
                    direction = 'right';
                else % polygon goes along the path
                    direction = 'parallel';
                end
                
                cut_smin = repmat(path_i1, 2, 1) + [zeros(2, 1); -v_j];
                cut_smax = repmat(path_i2, 2, 1) + [zeros(2, 1); -v_j];
                cut_tmin = path_i;
                cut_tmax = path_i + repmat(dt * -v_j, 2, 1);
                
                V = polygon_j;
                vid_i = 1:n_poly;
                
                switch direction
                    case 'left'
                        [C, ~, vid_i] = cut_polygon(V, vid_i, cut_tmin); V = [V C];
                        [C, vid_i, ~] = cut_multipolygon(V, vid_i, cut_tmax); V = [V C];
                        [C, vid_i, ~, vid_S_smin_i] = cut_multipolygon(V, vid_i, cut_smin); V = [V C];
                        [C, ~, vid_i, vid_S_smax_i] = cut_multipolygon(V, vid_i, cut_smax); V = [V C];
                    case 'right'
                        [C, vid_i, ~] = cut_polygon(V, vid_i, cut_tmin); V = [V C];
                        [C, ~, vid_i] = cut_multipolygon(V, vid_i, cut_tmax); V = [V C];
                        [C, ~, vid_i, vid_S_smin_i] = cut_multipolygon(V, vid_i, cut_smin); V = [V C];
                        [C, vid_i, ~, vid_S_smax_i] = cut_multipolygon(V, vid_i, cut_smax); V = [V C];
                    case 'parallel'
                        % TODO: implement
                        
                        error('nyi');
                end
                
                % determine relevant vids
                vid_all = unique([vid_i{:}]);
                % calculate old to new index mapping
                idx_map = inverse_order(vid_all);
                % translate indices
                V = V(:, vid_all);
                vid_i = cellfun(@(v) idx_map(v), vid_i, 'UniformOutput', false);
                vid_S_smin_i = idx_map(vid_S_smin_i);
                vid_S_smax_i = idx_map(vid_S_smax_i);
                
                e_s = vec_i / l(i);
                
                V_st_i = calc_polygon_st(V, path_i1, [s_i(i); t1], e_s, v_j);
                
                switch direction
                    case 'left' % polygon was mirrored
                        vid_i = cellfun(@fliplr, vid_i, 'UniformOutput', false);
                end
                
                V_st{i} = V_st_i;
                vid{i} = vid_i;
                vid_S_smin{i} = vid_S_smin_i;
                vid_S_smax{i} = vid_S_smax_i;
            end
                
            % TODO: glue polygons
            
            Om_st_j = cellfun(@vid2polygon, V_st, vid, 'UniformOutput', false);
            
            Om_st_j = [Om_st_j{:}];
        end
    end

    function V_st = calc_polygon_st(V, xy0, st0, e_s, v)
        n_V = size(V, 2);
        
        V_st = [e_s -v] \ (V - repmat(xy0, 1, n_V)) + repmat(st0, 1, n_V);
    end
end