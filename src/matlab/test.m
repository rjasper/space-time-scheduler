
[Os, Om] = obstacles_fixture;
I = [0 0]';
F = [12 12]';
% I = [-2 -2]';
% F = [ 2  2]';

[A, V] = vgraph(I, F, Os);
[d, pred] = dijkstra_sp(A, 1);
path = pred2path(pred, 2);

figure(1);
clf reset;
draw_graph(V, A);

figure(2);
clf reset;
draw_polygon(Os);
hold on;
draw_path(path, V);
hold off;