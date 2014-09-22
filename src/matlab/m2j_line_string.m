function [data, dim] = m2j_line_string(ls)

data = reshape(ls, 1, []);
dim = size(ls, 1);
