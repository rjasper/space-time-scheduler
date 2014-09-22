function Os = j2m_static_obstacles(data)

% TODO: check data

Os = cellfun(@j2m_polygon, data, 'UniformOutput', false);
