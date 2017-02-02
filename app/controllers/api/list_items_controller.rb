class Api::ListItemsController < ApiController
  before_action :set_list
  before_action :set_list_item, only: [:destroy]

  def create
    @list_item = @list.list_items.build(list_item_params)

    if @list_item.save
      render :show, status: :created
    else
      render json: @list_item.errors, status: :unprocessable_entity
    end
  end

  def destroy
    @list_item.destroy
    head :no_content
  end

  private

  def set_list
    @list = current_user.lists.find(params[:list_id])
  end

  def set_list_item
    @list_item = @list.list_items.find(params[:id])
  end

  def list_item_params
    params.require(:list_item).permit(:book_id)
  end
end
