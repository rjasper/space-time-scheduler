function l = path_length(V, path)

l = sqrt(sum( diff(V(:, path), 1, 2).^2 ));