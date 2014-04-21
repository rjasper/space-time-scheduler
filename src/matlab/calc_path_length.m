function l = calc_path_length(path)

l = sqrt( sum(diff(path, 1, 2).^2) );