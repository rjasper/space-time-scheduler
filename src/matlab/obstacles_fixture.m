function [Os, Om] = obstacles_fixture

Os = {
    regular_polygon(6, [ 4  4], 1)
    regular_polygon(3, [ 5 11], 3)
    regular_polygon(4, [ 9  8], 2)
    regular_polygon(4, [ 7  5], sqrt(2), pi/4)
    regular_polygon(3, [11  6], 2, deg2rad(180))
}';

Om = {
    struct( ...
        'polygon', [
            -3 -3  1  1  3  3;
             0 -2 -2  0  0  2], ...
        'path', [1 6 0; 4 0 3; 8 2 5; 6 6 7; 10 8 9; 8 12 15]')
}';