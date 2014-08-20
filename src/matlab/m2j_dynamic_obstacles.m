function [polygons_data, paths_data] = m2j_dynamic_obstacles(Os)

[polygons_data, paths_data] = cellfun(@m2j_dynamic_obstacle, Os, 'UniformOutput', false);
