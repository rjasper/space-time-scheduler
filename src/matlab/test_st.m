
Om = {
    struct( ...
        'polygon', regular_polygon(4, zeros(2, 1), 1, 0), ...
        'path', [1 0 0; 2 0 1]');
};

path = [0 0; 1 0; 2 0; 3 0]';

Om_st = calc_st_space(Om, path);

draw_polygon(Om_st, 'r');
