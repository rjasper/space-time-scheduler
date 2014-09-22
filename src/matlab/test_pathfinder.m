
%% data

t_start = 0;
t_end = 6;

I = [0; 0];
F = [10; 10];

v_max = 3;

Os = {
    regular_polygon(4, [4; 3], 2, deg2rad( 30))
    regular_polygon(3, [7; 8], 2, deg2rad(-30))
}';

Om = struct( ...
    'polygon', regular_polygon(4, [0; 0], 1, deg2rad(45)), ...
    'path', [[2; 6; 0] [8; 5; 5]]);

%% calculation

[xyt, evasions] = pathfinder(I, F, t_start, t_end, v_max, Os, Om);
[xyt_mt, evasions_mt] = pathfinder_mt(I, F, t_start, v_max, Os, Om);

%% plot

f1 = figure(1);
clf reset;
draw3D_obstacles(Os, Om, t_end);
hold on;
plot3(xyt(1, :), xyt(2, :), xyt(3, :), 'b');
plot3(xyt_mt(1, :), xyt_mt(2, :), xyt_mt(3, :), 'r');
hold off;
