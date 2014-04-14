function V = extract_vertices(Om)

N = size(Om, 2);

V = cell(1, N);

for i = 1:N
    V{i} = reshape(Om{i}, 2, []);
end

V = [V{:}]; % concatenate cells to single matrix