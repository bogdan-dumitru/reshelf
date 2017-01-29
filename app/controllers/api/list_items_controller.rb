class ListItemsController < ApplicationController
  before_action :set_list_item, only: [:show, :edit, :update, :destroy]

  def create
    @list_item = ListItem.new(list_item_params)

    if @list_item.save
      render :show, status: :created, location: @list_item
    else
      render json: @list_item.errors, status: :unprocessable_entity
    end
  end

  def destroy
    @list_item.destroy
    head :no_content
  end

  private

  def set_list_item
    @list_item = ListItem.find(params[:id])
  end

  def list_item_params
    params.require(:list_item).permit(:list_id, :goodreads_book_id)
  end
end
