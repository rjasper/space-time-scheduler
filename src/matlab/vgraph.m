function A = vgraph(Om)

V = extract_vertices(Om);
lines = extract_lines(Om);
[pids, vids, M] = extract_ids(Om);

N = size(V, 2);

A = zeros(N, N);

for i = 1:N
    for j = 1:N
        if i == j
            continue
        elseif pids(i) == pids(j)
            
        elseif visible(V(:, i), V(:, j), lines)
            A(i, j) = sqrt( sum( (V(:, i) - V(:, j)).^2 ) );
            
            line(V(1, [i j])', V(2, [i j])');
            hold on;
        end
    end
end

A = sparse(A);
