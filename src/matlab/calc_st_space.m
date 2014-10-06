function Om_st = calc_st_space(Om, path)

N_Om = size(Om, 2);

if N_Om == 0
    Om_st = cell(1, 0);
    return;
end

N_path = size(path, 2);
N_segs = N_path - 1;

v = cellfun(@calc_velocity, {Om.path}, 'UniformOutput', false);
l = calc_path_length(path);
s = cumsum([0 l]);

% debug
% j = 0;

Om_st = cellfun(@calc_Om_st, ...
    {Om.polygon}, ...
    {Om.path}, ...
    v, ...
    'UniformOutput', false);

        
% dirty hack check
if numel(Om_st) == 1 && numel(Om_st{1}) == 0
    Om_st = cell(1, 0);
end

    function v = calc_velocity(path)
        dpath = diff(path, 1, 2);
        
        % v := [dx; dy] / dt
        v = dpath(1:2, :) ./ repmat(dpath(3, :), 2, 1);
    end

    function Om_st = calc_Om_st(P, path_Om, v)
        n_v = size(v, 2);
        n_P = size(P, 2);
        
        p1 = path_Om(:, 1:end-1);
        p2 = path_Om(:, 2:end);
        
        [V_st, vid, vid_S_tmin, vid_S_tmax] = cellfun(@calc_Om_st_v, ...
            mat2cell( p1, 3, ones(1, n_v) ), ...
            mat2cell( p2, 3, ones(1, n_v) ), ...
            mat2cell( v , 2, ones(1, n_v) ), ...
            'UniformOutput', false);
        
        [V_st, vid] = merge_vertical(V_st, vid, vid_S_tmin, vid_S_tmax);
        
        % just for scoping :P
        function [V_, vid_] = merge_vertical(V, vid, vid_S_tmin, vid_S_tmax)
            if n_v == 1
                V_ = V{1};
                vid_ = vid{1};
            else
                V_L_st = V{1};
                vid_L = vid{1};
                vid_S_L = vid_S_tmax{1};

                for j = 2:n_v
                    V_R_st = V{j};
                    vid_R = vid{j};
                    vid_S_R = vid_S_tmin{j};

                    [V_L_st, vid_L, R2V_map] = merge_polygons(V_L_st, vid_L, vid_S_L, V_R_st, vid_R, fliplr(vid_S_R));
                    vid_S_L = R2V_map( vid_S_tmax{j} );
                end

                V_ = V_L_st;
                vid_ = vid_L;
            end
        end
        
        Om_st = vid2polygon(V_st,  vid);

%         Om_st = [Om_st{:}];
        
        function [V_st, vid, vid_S_tmin, vid_S_tmax] = calc_Om_st_v(p1, p2, v_j)
%             j = j + 1; % debug
            
            xy1 = p1(1:2);
            xy2 = p2(1:2);
            t1 = p1(3);
            t2 = p2(3);
            dt = t2 - t1; % TODO: already calculated by calc_velocity
            
            P_j = P + repmat(xy1, 1, n_P);
            
            V_st = cell(1, N_segs);
            vid = cell(1, N_segs);
            vid_S_smin = cell(1, N_segs);
            vid_S_smax = cell(1, N_segs);
            vid_S_tmin = cell(1, N_segs);
            vid_S_tmax = cell(1, N_segs);
            
            % for each path segment
            for i = 1:N_segs
                V = P_j;
                vid_i = 1:n_P;
                vid_S_smin_i = zeros(1, 0);
                vid_S_smax_i = zeros(1, 0);
                vid_S_tmin_i = zeros(1, 0);
                vid_S_tmax_i = zeros(1, 0);

                path_i1 = path(:, i); % segment start
                path_i2 = path(:, i+1); % segment end
                path_i = [path_i1; path_i2]; % path segment
                vec_i = line2vec(path_i);
                
                e_s = vec_i / l(i);
                
                if all(vec_i == 0) || all(v_j == 0)
                    alpha = 0;
                else
                    alpha = vec_vec_angle(vec_i, v_j);
                end
                
                if alpha == 0 || alpha == pi % TODO: define epsilon > 0
                    direction = 'parallel';
                elseif alpha < pi % polygon goes to the left
                    direction = 'left';
                else % alpha > pi % polygon goes to the right
                    direction = 'right';
                end
                
                switch direction
                    case 'parallel'
                        [~, ~, ~, vid_S, S_xT, B12] = cut_polygon(V, vid_i, path_i);
                        n_S = length(vid_S);
%                         isB12 = B(1, :) & B(2, :);
%                         B_idx = find(isB12);
                        B_idx = find(B12);

                        s1 = s(i) + S_xT;
                        s2 = s1 + dt * e_s' * v_j;

                        V_st_i = [s1 s2; repmat(t1, 1, n_S) repmat(t2, 1, n_S)];
                        
                        vid_i = cellfun( ...
                            @(b) [b b+1 n_S+b+1 n_S+b], num2cell(B_idx), ...
                            'UniformOutput', false);
                        
                        cut_tmin = [s(i  ) t1 s(i+1) t1]';
                        cut_tmax = [s(i+1) t2 s(i  ) t2]'; % flipped
                        cut_smin = [s(i  ) t2 s(i  ) t1]'; % flipped
                        cut_smax = [s(i+1) t1 s(i+1) t2]';
                        
                        % TODO: test vid_S_tmin_i and vid_S_tmax_i
                        
                        vid_S_tmin_i = 1:n_S;
                        vid_S_tmax_i = n_S + (n_S:-1:1); % flipped
                        
                        vid_S = {vid_S_tmin_i, vid_S_tmax_i};
                        
                        if i > 1
                            [C, vid_i_, ~, vid_S3, ~, B3, eid_C] = cut_multipolygon(V_st_i, vid_i, cut_smin);
%                             isB3 = B(1, :) & B(2, :);
                            
                            vid_S(1:2) = split_shared_vertices( ...
                                cut_smin, ...
                                [cut_tmin cut_tmax], ...
                                'll', ...
                                V_st_i, ...
                                vid_i, ...
                                vid_S(1:2), ...
                                eid_C, ...
... %                                 {isB12, isB12});
                                {B12, B12});
                            
                            V_st_i = [V_st_i C];
                            vid_i = vid_i_;
                            vid_S{3} = vid_S3;
                        else
                            vid_S{3} = zeros(1, 0);
%                             isB3 = false(1, 0);
                            B3 = false(1, 0);
                        end
                        
                        if i < N_segs
                            [C, vid_i_, ~, vid_S4, ~, B12, eid_C] = cut_multipolygon(V_st_i, vid_i, cut_smax);
%                             isB12 = B(1, :) & B(2, :);
                            
                            vid_S(1:3) = split_shared_vertices( ...
                                cut_smax, ...
                                [cut_tmin cut_tmax cut_smin], ...
                                'lll', ...
                                V_st_i, ...
                                vid_i, ...
                                vid_S(1:3), ...
                                eid_C, ...
... %                                 {isB12, isB12, isB3});
                                {B12, B12, B3});
                            
                            V_st_i = [V_st_i C];
                            vid_i = vid_i_;
                            vid_S{4} = vid_S4;
                        else
                            vid_S{4} = zeros(1, 0);
                        end
                        
                        [vid_S_tmin_i, vid_S_tmax_i, vid_S_smin_i, vid_S_smax_i] = vid_S{:};
                        
                        [V_st_i, vid_i, ~, vid_S_tmin_i, vid_S_tmax_i, vid_S_smin_i, vid_S_smax_i] = ...
                            vid_remap(V_st_i, vid_i, 0, vid_S_tmin_i, vid_S_tmax_i, vid_S_smin_i, vid_S_smax_i);
                    otherwise
                        if i == 1
                            cut_smin = NaN(4, 0);
                        else
                            cut_smin = repmat(path_i1, 2, 1) + [zeros(2, 1); -v_j];
                        end
                        
                        if i == N_segs
                            cut_smax = NaN(4, 0);
                        else
                            cut_smax = repmat(path_i2, 2, 1) + [zeros(2, 1); -v_j];
                        end
                        
                        cut_tmin = path_i;
                        cut_tmax = path_i + repmat(dt * -v_j, 2, 1);
                        
                        switch direction
                            case 'left'
                                cuts = [cut_tmax flipline(cut_smax) flipline(cut_tmin) cut_smin];
                            case 'right'
                                cuts = [cut_tmin cut_smax flipline(cut_tmax) flipline(cut_smin)];
                        end
                        
                        [C, vid_i, vid_S] = multicut_polygon(V, vid_i, cuts);
                        
                        if i == 1
                            vid_S = [vid_S {zeros(1, 0)}];
                        end
                        if i == N_segs
                            vid_S = [vid_S(1) {zeros(1, 0)} vid_S(2:end)];
                        end
                        
                        switch direction
                            case 'left'
                                [vid_S_tmax_i, vid_S_smax_i, vid_S_tmin_i, vid_S_smin_i] = fliplr_all( vid_S{:} );
                            case 'right'
                                [vid_S_tmin_i, vid_S_smax_i, vid_S_tmax_i, vid_S_smin_i] = vid_S{:};
                        end
                        
                        [V, vid_i, ~, vid_S_smin_i, vid_S_smax_i, vid_S_tmin_i, vid_S_tmax_i] = ...
                            vid_remap([V C], vid_i, 0, vid_S_smin_i, vid_S_smax_i, vid_S_tmin_i, vid_S_tmax_i);

                        V_st_i = calc_polygon_st(V, path_i1, [s(i); t1], e_s, v_j);

                        switch direction
                            case 'left' % polygon was mirrored
                                vid_i = cellfun(@fliplr, vid_i, 'UniformOutput', false);
                        end
                end

                V_st{i} = V_st_i;
                vid{i} = vid_i;
                vid_S_smin{i} = vid_S_smin_i;
                vid_S_smax{i} = vid_S_smax_i;
                vid_S_tmin{i} = vid_S_tmin_i;
                vid_S_tmax{i} = vid_S_tmax_i;
            end
            
            if N_segs == 1
                V_st = V_st{1};
                vid = vid{1};
                vid_S_tmin = vid_S_tmin{1};
                vid_S_tmax = vid_S_tmax{1};
            else
                V_L_st = V_st{1};
                vid_L = vid{1};
                vid_S_L = vid_S_smax{1};
                vid_S_tmin_L = vid_S_tmin{1};
                vid_S_tmax_L = vid_S_tmax{1};
                
                for i = 2:N_segs
                    V_R_st = V_st{i};
                    vid_R = vid{i};
                    vid_S_R = vid_S_smin{i};
                    vid_S_tmin_R = vid_S_tmin{i};
                    vid_S_tmax_R = vid_S_tmax{i};
                    
                    [V_L_st, vid_L, R2V_map] = merge_polygons(V_L_st, vid_L, vid_S_L, V_R_st, vid_R, fliplr(vid_S_R));
                    vid_S_L = R2V_map( vid_S_smax{i} );
                    vid_S_tmin_L = [vid_S_tmin_L, R2V_map( vid_S_tmin_R )];
                    vid_S_tmax_L = [R2V_map( vid_S_tmax_R ), vid_S_tmax_L];
                end
                
                V_st = V_L_st;
                vid = vid_L;
                vid_S_tmin = vid_S_tmin_L(diff([0, vid_S_tmin_L]) ~= 0);
                vid_S_tmax = vid_S_tmax_L(diff([0, vid_S_tmax_L]) ~= 0);
            end
            
%             Om_st_j = cellfun(@vid2polygon, V_st, vid, 'UniformOutput', false);
%             Om_st_j = vid2polygon(V_st, vid);
            
%             Om_st_j = [Om_st_j{:}];
        end
    end
end

function V_st = calc_polygon_st(V, xy0, st0, e_s, v)
    n_V = size(V, 2);

    V_st = [e_s -v] \ (V - repmat(xy0, 1, n_V)) + repmat(st0, 1, n_V);
end