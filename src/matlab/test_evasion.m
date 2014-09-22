
%% data

t_start = 0;
v_max = 1;

Os = {};

Om = struct( ...
    'polygon', {
        regular_polygon(4, [0; 0], 1, deg2rad(45))
        regular_polygon(3, [0; 0], 2, 0)}', ...
    'path', {
        [4 1.5 1; 4 10.5 22]', ...
        [9 6 8; 5 6 14]'});

V = [1 3; 7 3; 7 9; 1 9]';
path = 1:4;

%% calculation

Om_st = calc_st_space(Om, V(:, path));

unrolled = unroll(Om_st);
Om_st = unrolled(2, :);
idx_Om_st = [unrolled{1, :}];

L = sum(path_length(V, path));

I_st = [0 t_start]';

t_F = 12;
F_st = [L t_F]';

% [A_st, V_st] = directed_vgraph(I_st, F_st, Om_st, L, v_max);

[A_st, V_st, idx_F, pid] = minimum_time_vgraph(I_st, Om_st, L, v_max);
[d_st, pred_st] = dijkstra_sp(A_st, 1); % from I_st
[~, idx_dmin] = min(d_st(idx_F));
idx_F = idx_F(idx_dmin);
path_st = pred2path(pred_st, idx_F); % to F_st

% path_st = pred2path(pred_st, 2);

pid_path_st = pid(path_st);
evasions = unique(idx_Om_st(pid_path_st(pid_path_st > 0)));

F_st = V_st(:, idx_F);
t_F = F_st(2);

xyt = calc_xyt_path(V, path, V_st, path_st);

%% plot

f1 = figure(1);
clf reset;
draw3D_obstacles(Os, Om, t_F);
hold on;
plot3(xyt(1, :), xyt(2, :), xyt(3, :), 'b');
hold off;

f2 = figure(2);
clf reset;
draw_polygon(Om_st, 'g');
hold on;
draw_graph(V_st, A_st);
hold off;
