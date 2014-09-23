function [polygon_data, path_data, times_data] = m2j_dynamic_obstacle(Om)

polygon_data = m2j_polygon(Om.polygon);
path_data = Om.path(1:2, :);
times_data = Om.path(3, :);
