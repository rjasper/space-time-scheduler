function draw_graph(V, A)

[i, j] = find(A);
ij = [i j]';

N = length(i);

was_hold = ishold;

for k = 1:N
%     if i(k) > j(k)
%         continue
%     end

    V1 = V(:, ij(1, k));
    V2 = V(:, ij(2, k));
    dV = V2 - V1;
    
%     line(V(1, ij(:, k)')', V(2, ij(:, k)')');
    quiver(V1(1), V1(2), dV(1), dV(2), 0, 'b');
%     hold on;
end

% if ~was_hold
%     hold off;
% end