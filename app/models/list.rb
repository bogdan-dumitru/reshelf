class List < ApplicationRecord
  belongs_to :user
  has_many :list_items

  after_create do
    update!(color: "#f1cf74")
  end

  def book_id=(book_id)
    list_items.build({book_id: book_id})
  end
end
