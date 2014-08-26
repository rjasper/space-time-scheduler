function xyt = pathfinder(I, F, t_start, t_end, v_max, Os, Om)

[A, V] = vgraph(I, F, Os);
[d, pred] = dijkstra_sp(A, 1);
path = pred2path(pred, 2);
Om_st = calc_st_space(Om, V(:, path));

L = sum(path_length(V, path));

I_st = [0 t_start]';
F_st = [L t_end]';

[A_st, V_st] = directed_vgraph(I_st, F_st, Om_st, L, v_max);
[d_st, pred_st] = dijkstra_sp(A_st, 1); % from I_st
path_st = pred2path(pred_st, 2); % to F_st

xyt = calc_xyt_path(V, path, V_st, path_st);
