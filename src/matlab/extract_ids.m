function [pids, vids] = extract_ids(Om)

N = size(Om, 2);
M = zeros(1, N);
pids = cell(1, N);
vids = cell(1, N);

for i = 1:N
    M(i) = size(Om{i}, 2);
    
    pids{i} = repmat(i, 1, M);
    vids{i} = 1:M;
end

pids = [pids{:}]; % concatenate cells to single matrix
vids = [vids{:}]; % concatenate cells to single matrix