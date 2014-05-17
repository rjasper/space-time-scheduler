function Om_st = calc_st_space(Om, path)

N_Om = size(Om, 2);
N_path = size(path, 2);
N_segs = N_path - 1;

v = cellfun(@calc_velocity, {Om{:}.path}, 'UniformOutput', false);
l = calc_path_length(path);
s = cumsum([0 l]);

% debug
j = 0;

Om_st = cellfun(@calc_Om_st, ...
    {Om{:}.polygon}, ...
    {Om{:}.path}, ...
    v, ...
    'UniformOutput', false);

Om_st = [Om_st{:}];

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
        
        Om_st = cellfun(@calc_Om_st_v, ...
            mat2cell( p1, 3, ones(1, n_v) ), ...
            mat2cell( p2, 3, ones(1, n_v) ), ...
            mat2cell( v , 2, ones(1, n_v) ), ...
            'UniformOutput', false);
        
        Om_st = [Om_st{:}];
        
        function Om_st_j = calc_Om_st_v(p1, p2, v_j)
            j = j + 1;
            
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
                
                alpha = vec_vec_angle(vec_i, v_j);
                
                if alpha == 0 || alpha == pi % TODO: define epsilon > 0
                    direction = 'parallel';
                elseif alpha < pi % polygon goes to the left
                    direction = 'left';
                else % alpha > pi % polygon goes to the right
                    direction = 'right';
                end
                
                switch direction
                    case 'parallel'
                        [~, ~, ~, vid_S, S_xT, B] = cut_polygon(V, vid_i, path_i);
                        n_S = length(vid_S);
                        B_idx = find(B(1, :) & B(2, :));

                        s1 = s(i) + S_xT;
                        s2 = s1 + dt * e_s' * v_j;

                        V_st_i = [s1 s2; repmat(t1, 1, n_S) repmat(t2, 1, n_S)];
                        
                        vid_i = cellfun( ...
                            @(b) [b b+1 n_S+b+1 n_S+b], num2cell(B_idx), ...
                            'UniformOutput', false);
                        
                        cut_smin = [s(i  ) t2 s(i  ) t1]'; % flipped
                        cut_smax = [s(i+1) t1 s(i+1) t2]';
                        
                        % TODO: set vid_S_tmin_i and vid_S_tmax_i
                        
                        if i > 1
                            [C, ~, vid_i, vid_S_smin_i] = cut_multipolygon(V_st_i, vid_i, cut_smin); V_st_i = [V_st_i C];
                        end
                        if i < N_segs
                            [C, vid_i, ~, vid_S_smax_i] = cut_multipolygon(V_st_i, vid_i, cut_smax); V_st_i = [V_st_i C];
                        end
                        
                        [V_st_i, vid_i, ~, vid_S_smin_i, vid_S_smax_i] = ...
                            vid_remap(V_st_i, vid_i, 0, vid_S_smin_i, vid_S_smax_i);
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
%                                 [C, ~, vid_i] = cut_polygon(V, vid_i, cut_tmin); V = [V C];
%                                 [C, vid_i, ~] = cut_multipolygon(V, vid_i, cut_tmax); V = [V C];
%                                 if i > 1
%                                     [C, vid_i, ~, vid_S_smin_i] = cut_multipolygon(V, vid_i, cut_smin); V = [V C];
%                                 end
%                                 if i < N_segs
%                                     [C, ~, vid_i, vid_S_smax_i] = cut_multipolygon(V, vid_i, cut_smax); V = [V C];
%                                 end
                                
                                cuts = [cut_tmax; flipline(cut_smax); flipline(cut_tmin); cut_smin];
                            case 'right'
%                                 [C, vid_i, ~] = cut_polygon(V, vid_i, cut_tmin); V = [V C];
%                                 [C, ~, vid_i] = cut_multipolygon(V, vid_i, cut_tmax); V = [V C];
%                                 if i > 1
%                                     [C, ~, vid_i, vid_S_smin_i] = cut_multipolygon(V, vid_i, cut_smin); V = [V C];
%                                 end
%                                 if i < N_segs
%                                     [C, vid_i, ~, vid_S_smax_i] = cut_multipolygon(V, vid_i, cut_smax); V = [V C];
%                                 end
                                
                                cuts = [cut_tmin; cut_smax; flipline(cut_tmax); flipline(cut_smin)];
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
                                [vid_S_tmax_i, vid_S_smax_i, vid_S_tmin_i, vid_S_smin] = flipline( vid_S{:} );
                            case 'right'
                                [vid_S_tmin_i, vid_S_smax_i, vid_S_tmax_i, vid_S_smin] = vid_S{:};
                        end
                        
%                         [V, vid_i, ~, vid_S_smin_i, vid_S_smax_i] = ...
%                             vid_remap(V, vid_i, 0, vid_S_smin_i, vid_S_smax_i);

                        V_st_i = calc_polygon_st([V C], path_i1, [s(i); t1], e_s, v_j);

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
            else
                V_R_st = V_st{N_segs};
                vid_R = vid{N_segs};
                vid_S_R = vid_S_smin{N_segs}; % TODO: deal with flipping
                
                % TODO: switch direction (from first to last segment)
                for i = N_segs-1:-1:1
                    V_L_st = V_st{i};
                    vid_L = vid{i};
                    vid_S_L = vid_S_smax{i}; % TODO: deal with flipping
                    
                    [V_R_st, vid_R] = merge_polygons(V_L_st, vid_L, vid_S_L, V_R_st, vid_R, vid_S_R);
                    vid_S_R = vid_S_smin{i}; % TODO: deal with flipping
                end
                
                V_st = V_R_st;
                vid = vid_R;
            end
            
%             Om_st_j = cellfun(@vid2polygon, V_st, vid, 'UniformOutput', false);
            Om_st_j = vid2polygon(V_st, vid);
            
%             Om_st_j = [Om_st_j{:}];
        end
    end
end

function V_st = calc_polygon_st(V, xy0, st0, e_s, v)
    n_V = size(V, 2);

    V_st = [e_s -v] \ (V - repmat(xy0, 1, n_V)) + repmat(st0, 1, n_V);
end