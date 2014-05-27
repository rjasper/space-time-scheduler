function vec = calc_line_vec(line)
% TODO: identical implementation to line2vec

vec = line(3:4, :) - line(1:2, :);

end