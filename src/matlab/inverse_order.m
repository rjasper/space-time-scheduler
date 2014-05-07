function inverse = inverse_order(order)
    
n = size(order, 2);

zero_filt = order == 0;
has_zeros = any(zero_filt);

if has_zeros
    order(zero_filt) = max(order)+1;
end

inverse(order) = 1:n;

if has_zeros
    inverse = inverse(1:end-1);
end