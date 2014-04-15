function vec = line2vec(line)

if iscell(line)
    vec = cellfun(@(l) helper(l), line, 'UniformOutput', false);
else
    vec = helper(line);
end

function vec = helper(line)

vec = line(3:4, :) - line(1:2, :);