function Os = j2m_dynamic_obstacles(polygons_data, paths_data)

Os = cellfun(@j2m_dynamic_obstacle, polygons_data, paths_data, 'UniformOutput', false);
