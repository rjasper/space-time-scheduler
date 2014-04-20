function [Os, Om] = obstacles_fixture

Os = {
%     regular_polygon(4, [-1 -1], 1, 0)
%     regular_polygon(4, [ 1  1], 1, 0)
    regular_polygon(6, [ 4  4], 1)
    regular_polygon(3, [ 5 11], 3)
    regular_polygon(4, [ 9  8], 2)
    regular_polygon(4, [ 7  5], sqrt(2), pi/4)
    regular_polygon(3, [11  6], 2, deg2rad(180))
%     regular_polygon(4, [ 4  8], 2)
%     regular_polygon(4, [ 6  8], 2)
}';

Om = {
    struct( ...
        'polygon', regular_polygon(4, zeros(2, 1), 2, 0), ...
        'path', [4 8 0; 6 8 2; 4 8 4]')
}';