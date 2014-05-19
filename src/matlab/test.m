
[Os, Om] = obstacles_fixture;
% I = [0 0]';
% F = [11 10]';

I = [-2 -2]';
F = [ 2  2]';

t_F = 15.5;
v_max = 3;

% [A, V] = vgraph(I, F, Os);
% [d, pred] = dijkstra_sp(A, 1);
% path = pred2path(pred, 2);
% save('pp', 'V', 'A', 'path');

% pp = load('pp');
% A = pp.A;
% V = pp.V;
% path = pp.path;

V = [
     0 2 2 4 4 6 8 8 6  8;
    -1 1 3 3 5 3 5 7 9 11;
];

path = 1:length(V);

L = sum(path_length(V, path));

I_st = [0 0]';
F_st = [L t_F]';

Om_st = calc_st_space(Om, V(:, path));

% [A_st, V_st] = directed_vgraph(I_st, F_st, Om_st, L, v_max);
% [d_st, pred_st] = dijkstra_sp(A_st, 1); % from I_st
% path_st = pred2path(pred_st, 2); % to F_st
% 
% XYT = calc_xyt_path(V, path, V_st, path_st);

% figure(1);
% clf reset;
% draw_graph(V, A);

% figure(2);
% clf reset;
% draw_polygon(Os, 'r');
% % draw_polygon({
% %     regular_polygon(4, [ 4  8], 2)
% %     regular_polygon(4, [ 6  8], 2)
% % }');
% hold on;
% draw_path(path, V);
% hold off;

figure(3);
clf reset;
draw_polygon(Om_st, 'g');
hold on;
draw_path(path_st, V_st);
hold off;

% figure(4);
% % draw_polygon(Om_st, 'g');
% hold on;
% draw_graph(V_st, A_st);
% hold off;


% figure(5);
% clf reset;
% % draw3D_obstacles(Os, Om, t_F);
% draw3D_obstacles({}, Om, t_F);
% hold on;
% plot3(XYT(1, :), XYT(2, :), XYT(3, :));
% hold off;
