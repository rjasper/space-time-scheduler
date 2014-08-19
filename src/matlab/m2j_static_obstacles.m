function data = m2j_static_obstacles(Os)

% TODO: check Os

data = cellfun(@m2j_polygon, Os, 'UniformOutput', false);
