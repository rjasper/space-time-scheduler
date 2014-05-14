function l = calc_line_length(line)

n = size(line, 2);
vec = calc_line_vec(line);

l = NaN(1, n);

parfor i = 1:n
    l(i) = norm(vec(:, i));
end

end