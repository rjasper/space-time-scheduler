function [polygons_data, paths_data, times_data] = m2j_dynamic_obstacles(Os)

[polygons_data, paths_data, times_data] = arrayfun(@m2j_dynamic_obstacle, ...
    Os, 'UniformOutput', false);
