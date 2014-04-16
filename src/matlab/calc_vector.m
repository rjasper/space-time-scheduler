function vec = calc_vector(P)

if iscell(P)
    vec = cellfun(@helper, P, 'UniformOutput', false);
else
    vec = helper(P);
end

function vec = helper(P)

vec = diff([P P(:, 1)], 1, 2);