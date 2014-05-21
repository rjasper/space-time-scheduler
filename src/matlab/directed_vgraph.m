function [A_st, V_st] = directed_vgraph(I_st, F_st, Om_st, s_max, v_max)

V_st = [I_st F_st [Om_st{:}]];
l = get_edges(Om_st);
l = [l{:}];
[pid, vid, n] = get_ids(Om_st);
pid = [0 0 pid];
vid = [1 2 vid];
n = [2 2 n];

vec = calc_vector(Om_st);
rho = calc_vector_angle(vec);
alpha = calc_vertex_angle(rho);

n_V = size(V_st, 2);

A_st = zeros(n_V, n_V);


for i = 1:n_V
    if V_st(1, i) < 0 || V_st(1, i) > s_max %% TODO: use eps
        continue;
    end
    
    for j = 1:n_V
        if i == j
            continue
        end
        
        if V_st(1, j) < 0 || V_st(1, j) > s_max %% TODO: use eps
            continue;
        end
        
        % t_i >= t_j
        if V_st(2, i) >= V_st(2, j)
            continue;
        end
        
        dV = V_st(:, i) - V_st(:, j);
        % |ds / dt| > v_max
        if abs(dV(1) / dV(2)) > v_max
            continue;
        end
        
        if pid(i) == 0
            filt_i = true(1, n_V);
        else
            filt_i = pid ~= pid(i) | (vid ~= pred(i) & vid ~= vid(i));
        end
        
        if pid(j) == 0
            filt_j = true(1, n_V);
        else
            filt_j = pid ~= pid(j) | (vid ~= pred(j) & vid ~= vid(j));
        end
        
        l_filt = l(:, filt_i(3:end) & filt_j(3:end));
        
        % if neighbors
        if pid(i) ~= 0 && pid(i) == pid(j) && (pred(i) == vid(j) || pred(j) == vid(i))
            A_st(i, j) = sqrt( sum( (V_st(:, i) - V_st(:, j)).^2 ) );
        elseif visible(V_st(:, i), V_st(:, j), l_filt) % check line of sight
            % if both vertices belong to the same polygon then check visibility
            if pid(i) ~= 0 && pid(i) == pid(j) && ~visible2(Om_st, rho, alpha, pid(i), vid(i), vid(j))
                continue
            end
            
            % TODO: weight does not make sense
            
            % distance between the two vertices
            A_st(i, j) = sqrt( sum( (V_st(:, i) - V_st(:, j)).^2 ) );
        end
    end
end

A_st = sparse(A_st);

    function pred_vid = pred(i)
        vid_i = vid(i);
        n_i = n(i);
        
        if vid_i == 1
            pred_vid = n_i;
        else
            pred_vid = vid_i-1;
        end
    end

    function succ_vid = succ(i)
        vid_i = vid(i);
        n_i = n(i);
        
        if vid_i == n_i
            succ_vid = 1;
        else
            succ_vid = vid+1;
        end
    end
end