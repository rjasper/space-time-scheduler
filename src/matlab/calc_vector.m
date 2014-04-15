function vec = calc_vector(Om)

if iscell(Om)
    vec = cellfun(@helper, Om, 'UniformOutput', false);
else
    vec = helper(P);
end

function vec = helper(P)

vec = diff([P P(:, 1)], 1, 2);