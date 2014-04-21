function alpha = vec_vec_angle(vec1, vec2)

alpha1 = atan2(vec1(2, :), vec1(1, :));
alpha2 = atan2(vec2(2, :), vec2(1, :));

alpha = mod( alpha2 - alpha1, 2*pi );