function draw3D_obstacles(Os, Om, t_max)

N_Os = length(Os);
path_Os = repmat({[0 0 0; 0 0 t_max]'}, 1, N_Os);

draw3D_polygon(Os, path_Os, 'FaceColor', 'r');
draw3D_polygon({Om{:}.polygon}, {Om{:}.path}, 'FaceColor', 'c');

end