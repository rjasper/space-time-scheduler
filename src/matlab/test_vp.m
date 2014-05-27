%% reset

close all;
clear variables;

%% definitions

t_F = 15.5;
v_max = 3;

V = [
     0 2 2 4 4 6 8 8 6  8;
    -1 1 3 3 5 3 5 7 9 11;
];

path = 1:length(V);

[~, Om] = obstacles_fixture;

%% preparations

L = sum(path_length(V, path));

I_st = [0 0]';
F_st = [L t_F]';

%% calculations

Om_st = calc_st_space(Om, V(:, path));

[A_st, V_st] = directed_vgraph(I_st, F_st, Om_st, L, v_max);
[d_st, pred_st] = dijkstra_sp(A_st, 1); % from I_st
path_st = pred2path(pred_st, 2); % to F_st

XYT = calc_xyt_path(V, path, V_st, path_st);

%% plot

figure(1);
clf reset;
    draw_polygon(Om_st, 'g');
hold on;
    draw_graph(V_st, A_st);
hold off;

figure(2);
clf reset;
    draw_polygon(Om_st, 'g');
hold on;
    draw_path(path_st, V_st);
hold off;

figure(3);
clf reset;
    draw3D_obstacles({}, Om, t_F);
hold on;
    plot3(XYT(1, :), XYT(2, :), XYT(3, :));
hold off;
