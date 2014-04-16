function vec = line2vec(l)

if iscell(l)
    vec = cellfun(@helper, l, 'UniformOutput', false);
else
    vec = helper(l);
end

function vec = helper(l)

vec = l(3:4, :) - l(1:2, :);