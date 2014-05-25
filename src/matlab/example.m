%% data

I = [0; 0];
F = [10; 10];

t_F = 5;
v_max = 4;

Os = {
    regular_polygon(4, [4; 3], 2, deg2rad( 30))
    regular_polygon(3, [7; 8], 2, deg2rad(-30))
}';

P1 = regular_polygon(4, [2; 6], 1, deg2rad(45));
P2 = regular_polygon(4, [8; 5], 1, deg2rad(45));

Om = {
    struct( ...
        'polygon', regular_polygon(4, [0; 0], 1, deg2rad(45)), ...
        'path', [[2; 6; 0] [8; 5; 5]])
}';

%% calculation

[A, V] = vgraph(I, F, Os);
[d, pred] = dijkstra_sp(A, 1);
path = pred2path(pred, 2);
Om_st = calc_st_space(Om, V(:, path));

% l = sqrt(sum(diff(V(:, path), 1, 2).^2));
L = sum(path_length(V, path));

I_st = [0 0]';
F_st = [L t_F]';

[A_st, V_st] = directed_vgraph(I_st, F_st, Om_st, L, v_max);
[d_st, pred_st] = dijkstra_sp(A_st, 1); % from I_st
path_st = pred2path(pred_st, 2); % to F_st

XYT = calc_xyt_path(V, path, V_st, path_st);

%% plot

% f1 = figure(1);
% clf reset;
% hold on;
% draw_polygon(Os, 'r');
% % draw_polygon({P1 P2}, 'b');
% hold off;
% axis equal;
% axis([0 10 0 10]);

% f2 = figure(2);
% clf reset;
% hold on;
% draw_polygon(Os, 'r');
% draw_polygon({P1 P2}, 'b');
% draw_path(path, V);
% hold off;
% axis equal;
% axis([0 10 0 10]);

% f3 = figure(3);
% clf reset;
% hold on;
% draw_graph(V, A);
% % draw_polygon({P1 P2}, 'b');
% hold off;
% axis equal;
% axis([0 10 0 10]);

% f4 = figure(4);
% clf reset;
% hold on;
% draw_polygon(Os, 'r');
% draw_path(path, V);
% hold off;
% axis equal;
% axis([0 10 0 10]);

f5 = figure(5);
clf reset;
draw_polygon(Om_st, 'g');
hold on;
draw_graph(V_st, A_st);
hold off;
axis([0 L 0 5]);

f6 = figure(6);
clf reset;
draw3D_obstacles(Os, Om, t_F);
hold on;
plot3(XYT(1, :), XYT(2, :), XYT(3, :), 'b');
hold off;

%% save

% saveas(f1, 'static_obstacles.png');
% saveas(f2, 'all_obstacles.png');
% saveas(f3, 'vgraph.png');
% saveas(f4, 'path.png');
% saveas(f5, 'st_space.png');
saveas(f5, 'st_edges.png');
