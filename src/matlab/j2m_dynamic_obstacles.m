function Om = j2m_dynamic_obstacles(polygons_data, paths_data, times_datas)

Om = cellfun(@j2m_dynamic_obstacle, polygons_data, paths_data, times_datas);
