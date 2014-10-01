function [xyt, evasions] = pathfinder_mt(I, F, t_start, t_end_min, t_end_max, v_max, t_spare, Os, Om)

[A, V] = vgraph(I, F, Os);
[d, pred] = dijkstra_sp(A, 1);
path = pred2path(pred, 2);

if numel(Om) == 0
    Om_st = cell(1, 0);
    idx_Om_st = zeros(1, 0);
else
    Om_st = calc_st_space(Om, V(:, path));

    unrolled = unroll(Om_st);
    Om_st = unrolled(2, :);
    idx_Om_st = [unrolled{1, :}];
end

L = sum(path_length(V, path));

I_st = [0 t_start]';

[A_st, V_st, idx_F, pid] = minimum_time_vgraph(I_st, Om_st, L, t_end_min, t_end_max, v_max, t_spare);
[d_st, pred_st] = dijkstra_sp(A_st, 1); % from I_st
[d_min, idx_dmin] = min(d_st(idx_F));

if d_min < Inf
    idx_F = idx_F(idx_dmin);
    path_st = pred2path(pred_st, idx_F); % to F_st
else
    path_st = zeros(1, 0);
end

pid_path_st = pid(path_st);
evasions = unique(idx_Om_st(pid_path_st(pid_path_st > 0)));

% F_st = V_st(:, idx_F);
% t_F = F_st(2);

xyt = calc_xyt_path(V, path, V_st, path_st);
