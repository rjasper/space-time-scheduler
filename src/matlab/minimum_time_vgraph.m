function [A_st, V_st, idx_F, pid] = minimum_time_vgraph(I_st, Om_st, s_max, t_max, v_max, t_spare)

V_Om = [Om_st{:}];
n_V_Om = size(V_Om, 2);
V_st = [I_st V_Om];
n_V = size(V_st, 2);
l = get_edges(Om_st);
l = [l{:}];
[pid_Om, vid_Om, n_Om] = get_ids(Om_st);
pid = [0 pid_Om];
vid = [1 vid_Om];
n = [1 n_Om];

vec = calc_vector(Om_st);
rho = calc_vector_angle(vec);
alpha = calc_vertex_angle(rho);

idx_Om_st = 2:n_V_Om+1;

[F_st, idx_pred] = determine_final_nodes;
n_F = size(F_st, 2);
V_st = [I_st V_Om F_st];
pid = [pid zeros(1, n_F)];
vid = [vid 2:(n_F+1)];
n = [n_F+1 n_Om repmat(n_F+1, 1, n_F)];

n_V = size(V_st, 2);
A_st = zeros(n_V, n_V);

for i = 1:n_V_Om+1
    if V_st(1, i) < 0 || V_st(1, i) > s_max %% TODO: use eps
        continue;
    end
    
    for j = 1:n_V_Om+1
%     for j = 1:n_V
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
        
        if neighbors(i, j) || visible_(i, j)
            % A_st(i, j) = t_j - t_i
            A_st(i, j) = diff(V_st(2, [i j]));
        end
    end
end

idx_F = 2+n_V_Om:n_V;

% calculation of edges to final nodes
A_st(sub2ind(size(A_st), idx_pred, idx_F)) = F_st(2, :) - V_st(2, idx_pred);

A_st = sparse(A_st);

    function [F, idx_pred] = determine_final_nodes
        s = [I_st(1) V_Om(1, :)];
        t = [I_st(2) V_Om(2, :)];
    
        % check bounds
        filt = s >= 0 & s <= s_max & t <= t_max;
        idx = find(filt);
        n_ = size(idx, 2);
        
        % calc final points
        s_F = repmat(s_max, 1, n_);
        t_F = t(filt) + (s_max - s(filt)) / v_max;
        F = [s_F; t_F];
        
        % check spare time
        vids = calc_vids(Om_st);
        [~, ~, ~, ~, S_xT, B, ~] = ...
            cut_multipolygon(V_Om, vids, [s_max 0 s_max 1]');
        % bridge starts and ends
        b1 = S_xT(B(1:end));
        S_xT_ = S_xT(2:end);
        b2 = S_xT_(B(1:end-1));
        
        filt = false(1, n_);
        for k = 1:n_
            % does time interval (t, t+t_spare) collide with obstacle?
            filt(k) = ~any(t_F(k) < b2 & t_F(k) + t_spare > b1);
        end
        idx = idx(filt);
        n_ = size(idx, 2);
        F = F(:, filt);
        
        % check visibility
        filt_l = cellfun(@(i) pid ~= pid(i) | (vid ~= pred(i) & vid ~= vid(i)), ...
            num2cell(idx), ...
            'UniformOutput', false);
        filt = cellfun(@(V_Om, F, filt_l, pid_i, vid_i) ...
            visible(V_Om, F, l(:, filt_l(2:end))) && (pid_i == 0 || visible2_(pid_i, vid_i, F)), ...
            mat2cell(V_st(:, idx), 2, ones(1, n_)), ...
            mat2cell(F, 2, ones(1, n_)), ...
            filt_l, ...
            num2cell(pid(idx)), ...
            num2cell(vid(idx)));
        idx = idx(filt);
        
        F = F(:, filt);
        idx_pred = idx;
    end

    function b = neighbors(i, j)
        b = pid(i) ~= 0 && pid(i) == pid(j) && (pred(i) == vid(j) || pred(j) == vid(i));
    end

    function b = visible_(i, j)
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
        
        l_filt = l(:, filt_i(idx_Om_st) & filt_j(idx_Om_st));
        
        if visible(V_st(:, i), V_st(:, j), l_filt)
            b = not( pid(i) ~= 0 && pid(i) == pid(j) && ~visible2(Om_st, rho, alpha, pid(i), vid(i), vid(j)) );
        else
            b = false;
        end
    end

    function b = visible2_(pid, vid1, P2)
        P1 = Om_st{pid}(:, vid1);

        % angle of the second line of vertex 1
        rho2 = rho{pid}(vid1);
        % angle of line between P1 and P2
        rho_ = calc_line_angle([P1; P2]);

        % check if rho lies between rho1 and rho2
        % where rho1 is the first and rho2 is the second line of vertex 1

        % angle between rho2 and rho
        delta1 = mod(rho_ - rho2, 2*pi);
        % angle between rho2 and rho1
        delta2 = alpha{pid}(vid1);

        b = not( 0 < delta1 && delta1 < delta2 );
    end

    function pred_vid = pred(i)
        vid_i = vid(i);
        n_i = n(i);
        
        if vid_i == 1
            pred_vid = n_i;
        else
            pred_vid = vid_i-1;
        end
    end

    function vids = calc_vids(P)
        n_P = cellfun(@(p) size(p, 2), P);
        offset = cumsum([0 n_P(1:end-1)]);
        vids = arrayfun(@(n_P, offset) (1:n_P)+offset, ...
            n_P, offset, ...
            'UniformOutput', false);
    end
end