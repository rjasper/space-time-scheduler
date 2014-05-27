function draw_graph(V, A)

[i, j] = find(A);

N = length(i);

was_hold = ishold;

for k = 1:N

    V1 = V(:, i(k));
    V2 = V(:, j(k));
    dV = V2 - V1;
    
    quiver(V1(1), V1(2), dV(1), dV(2), 0, 'b');
    hold on;
end

if ~was_hold
    hold off;
end