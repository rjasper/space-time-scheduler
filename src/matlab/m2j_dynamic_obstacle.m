function [polygon_data, path_data] = m2j_dynamic_obstacle(Os)

polygon_data = m2j_polygon(Os.polygon);
path_data = m2j_line_string(Os.path);
