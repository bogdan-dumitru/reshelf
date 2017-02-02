json.extract! list, :id, :name, :position, :color
json.list_items do
  json.array! list.list_items, partial: 'api/list_items/list_item', as: :list_item
end
