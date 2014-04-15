
Om = obstacles_fixture;
I = [0 0]';
F = [12 12]';

[A, V] = vgraph(I, F, Om);
[d, pred] = dijkstra_sp(A, 1);
path = pred2path(pred, 2);

draw_obstacle(Om);
hold on;
draw_path(path, V);
hold off;