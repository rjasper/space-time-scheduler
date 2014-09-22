function [xyt, evasions] = pathfinder(I, F, t_start, t_end, v_max, Os, Om)

[A, V] = vgraph(I, F, Os);
[d, pred] = dijkstra_sp(A, 1);
path = pred2path(pred, 2);
Om_st = calc_st_space(Om, V(:, path));

unrolled = unroll(Om_st);
Om_st = unrolled(2, :);
idx_Om_st = [unrolled{1, :}];

L = sum(path_length(V, path));

I_st = [0 t_start]';
F_st = [L t_end]';

[A_st, V_st, pid] = directed_vgraph(I_st, F_st, Om_st, L, v_max);
[d_st, pred_st] = dijkstra_sp(A_st, 1); % from I_st

if d_st(2) < Inf
    path_st = pred2path(pred_st, 2); % to F_st
else
    path_st = zeros(1, 0);
end

pid_path_st = pid(path_st);
evasions = unique(idx_Om_st(pid_path_st(pid_path_st > 0)));

xyt = calc_xyt_path(V, path, V_st, path_st);
