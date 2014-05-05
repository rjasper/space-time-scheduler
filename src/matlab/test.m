
[Os, Om] = obstacles_fixture;
I = [0 0]';
F = [11 10]';
% I = [-2 -2]';
% F = [ 2  2]';

[A, V] = vgraph(I, F, Os);
[d, pred] = dijkstra_sp(A, 1);
path = pred2path(pred, 2);
save('pp', 'V', 'A', 'path');

% pp = load('pp');
% A = pp.A;
% V = pp.V;
% path = pp.path;

% V = [
%     0 2 2 4 4 6 8 8 6  8;
%     0 1 3 3 5 3 5 7 9 11;
% ];
% 
% path = 1:length(V);

% Om_st = calc_st_space(Om, V(:, path));

figure(1);
clf reset;
draw_graph(V, A);

figure(2);
clf reset;
draw_polygon(Os, 'r');
% draw_polygon({
%     regular_polygon(4, [ 4  8], 2)
%     regular_polygon(4, [ 6  8], 2)
% }');
hold on;
draw_path(path, V);
hold off;

% figure(3);
% clf reset;
% draw_polygon(Om_st);