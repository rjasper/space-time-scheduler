function Om = j2m_dynamic_obstacles(polygons_data, paths_data, times_datas)

n = numel(polygons_data);

if n == 0
    Om = struct( ...
        'polygon', cell(1, 0), ...
        'path'   , cell(1, 0));
else
    Om = cellfun(@j2m_dynamic_obstacle, polygons_data, paths_data, times_datas);
end
