function draw_graph(V, A)

[i, j] = find(A);
ij = [i j]';

N = length(i);

was_hold = ishold;

for k = 1:N
%     if i(k) > j(k)
%         continue
%     end
    
    line(V(1, ij(:, k)')', V(2, ij(:, k)')');
    hold on;
end

if ~was_hold
    hold off;
end