function rho = calc_vector_angle(vec)

if iscell(vec)
    rho = cellfun(@helper, vec, 'UniformOutput', false);
else
    rho = helper(vec);
end

function rho = helper(vec)

rho = atan2(vec(2, :), vec(1, :));