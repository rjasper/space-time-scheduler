
[Os, Om] = obstacles_fixture;
I = [0 0]';
F = [11 10]';
% I = [-2 -2]';
% F = [ 2  2]';

% [A, V] = vgraph(I, F, Os);
% [d, pred] = dijkstra_sp(A, 1);
% path = pred2path(pred, 2);
% save('pp', 'V', 'A', 'path');

pp = load('pp');
A = pp.A;
V = pp.V;
path = pp.path;

calc_st_space(Om, V(:, path));

figure(1);
clf reset;
draw_graph(V, A);

figure(2);
clf reset;
draw_polygon(Os);
hold on;
draw_path(path, V);
hold off;
