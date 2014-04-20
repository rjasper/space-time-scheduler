function [pid, vid, n] = get_ids(Os)

N = size(Os, 2);
% M = zeros(1, N);
pid = cell(1, N);
vid = cell(1, N);
n = cell(1, N);

for i = 1:N
    M_i = size(Os{i}, 2);
    
    pid{i} = repmat(i, 1, M_i);
    vid{i} = 1:M_i;
    n{i} = repmat(M_i, 1, M_i);
end

pid = [pid{:}]; % concatenate cells to single matrix
vid = [vid{:}]; % concatenate cells to single matrix
n = [n{:}];