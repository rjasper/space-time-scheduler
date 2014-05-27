%% reset

close all;
clear variables;

%% definitions

I = [0 0]';
F = [11 10]';

[Os, ~] = obstacles_fixture;

%% calculations

[A, V] = vgraph(I, F, Os);
[d, pred] = dijkstra_sp(A, 1);
path = pred2path(pred, 2);

%% plot

figure(1);
clf reset;
    draw_graph(V, A);

figure(2);
clf reset;
    draw_polygon(Os, 'r');
hold on;
    draw_path(path, V);
hold off;
