function Om = j2m_dynamic_obstacle(polygon_data, path_data, times_data)

path = j2m_line_string(path_data, 2);
times = times_data;
path3D = [path; times];

Om = struct( ...
    'polygon', j2m_polygon(polygon_data), ...
    'path'   , path3D ...
);
